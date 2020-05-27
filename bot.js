require('dotenv').config();
require('node-opus');
const say = require('say');
const discord = require('discord.js');
const client = new discord.Client;
const dialogflow = require('dialogflow');
const uuid = require('uuid');
const pump = require('pump');
const reqJSON = require('request-json');
const through2 = require('through2');
//settings for the request send to Google Cloud.
const encoding = 'AUDIO_ENCODING_LINEAR_16';
const sampleRateHertz = 48000;
const languageCode = 'en-US';
const projectId = 'stopwatch-242419';
const {
  Readable,
  Transform
} = require('stream');

const defaultMsgAudioPath = './msg.wav';

//map keying from voice channel id -> hashmap containing the timers for that match.
//essentially, each voice channel can only be timing for a single match. this allows the bot
//to be used on multiple servers simultaneously.
let channelSpecificTimers = new Map();

/**
 * Class representing silent audio to be sent by the bot. I could only get
 * others' speech audio if the bot itself was transmitting audio.
 */
class Silence extends Readable {
  _read() {
    this.push(Buffer.from([0xF8, 0xFF, 0xFE]));
  }
}

// Function and class for converting the Discord input audio to
// single stream audio. Didn't come up with these myself, got them from
// this article:
// https://refruity.xyz/writing-discord-bot/
function convertBufferTo1Channel(buffer) {
  const convertedBuffer = Buffer.alloc(buffer.length / 2);
  for (let i = 0; i < convertedBuffer.length / 2; i++) {
    const uint16 = buffer.readUInt16LE(i * 4);
    convertedBuffer.writeUInt16LE(uint16, i * 2)
  }
  return convertedBuffer
}

class ConvertTo1ChannelStream extends Transform {
  constructor(source, options) {
    super(options)
  }

  _transform(data, encoding, next) {
    next(null, convertBufferTo1Channel(data))
  }
}

async function playFile(connection, filePath) {
  return new Promise((resolve, reject) => {
    const dispatcher = connection.play(filePath)
    dispatcher.setVolume(1)
    dispatcher.on('start', () => {
      console.log('Playing')
    })
    dispatcher.on('end', () => {
      resolve()
    })
    dispatcher.on('error', (error) => {
      console.error(error)
      reject(error)
    })
  })
}

client.on('message', msg => {
  if (msg.mentions.members !== null && msg.mentions.members.has(client.user.id)) {
    let helpReg = new RegExp("^<@!582701817121603604>\\s+(.+)");
    if (helpReg.test(msg.content)) {
      let query = helpReg.exec(msg.content)[1];
      if (query === 'help') {
        msg.author.send("Hi! I'm Stopwatch, a Discord bot for timing summoner spells in League of Legends. I'm currently only functional for NA servers.\n", {embed: {
            color: 3447003,
            author: {
              name: client.user.username,
              icon_url: client.user.avatarURL
            },
            fields: [{
                name: "Telling the bot to monitor your game",
                value: "To use me, make sure you're in game (Summoner's Rift or ARAM only) and use the command ```!timer <summoner name>``` to start timers."
              },
              {
                name: "Starting spell timers",
                value: "From there, you can time summoner spells by saying phrases such as ```Draven no flash``` or ```Corki just flashed.``` Full champion names will be detected more accurately. "
              },
              {
                name: "Asking for spell cooldowns",
                value: "You can ask the bot if a champion has a summoner spell by saying a phrase such as ```Does Garen have flash?``` or ```Does Mordekaiser have TP?```"
              },

            ],
            timestamp: new Date(),
          }
        })
      }
    }
  }

  let timerReg = new RegExp("^!timer\\s+(.+)");
  if (timerReg.test(msg.content)) {
    let matches = timerReg.exec(msg.content);
    let summName = matches[1];
    if (msg.member.voice.channel == null) {
      msg.reply("you need to join a voice channel before I can start timing.");
    } else {
      console.log("he in");
      setupRiotApi(summName, msg);
    }
  } else if (msg.content === 'leave') {
    msg.member.voice.channel.leave();
  } else if (msg.content === 'quit') {
    console.log("destroyed");
    client.destroy();
  }
});

client.login(process.env.DISCORD_KEY);

/**
 * Function that tells the Java backend to retrieve
 * game data info for the given summoner.
 * @param summName the summoner name
 * @param msg the Discord message telling the bot to start
 */
function setupRiotApi(summName, msg) {
  //makes HTTP request to backend, initializing objects that
  //will use Riot's API to track game data
  let getUrl = "http://localhost:6789/";
  let reqClient = reqJSON.createClient(getUrl);
  let getParams = {
    summName: summName,
    voiceChannel: msg.member.voice.channelID,
  };
  reqClient.post("/gameStart", getParams, function(err, res, body) {
    if (body.statusCode === "success") {
      msg.reply("I'll be timing spells for " + summName + "! GLHF.");
      channelSpecificTimers.set(msg.member.voice.channelID, new Map());
      connectToVoice(msg.member, msg);
    } else {
      console.log(body);
      msg.reply(body.statusMsg);
    }
  });
}

/**
 * Connects the bot to the voice channel and starts sending
 * voice data to Dialogflow.
 * @param member the member of the Discord asking for timers
 * @param msg the message telling the bot to start timing
 */
function connectToVoice(member, msg) {
  let channel = member.voice.channel;
  if (channel != null) {
    channel.join()
      .then(function(connection) {
        console.log('Connected!');
        let readable = connection.receiver;
        const sessionId = uuid.v4();

        playFile(connection, './wrongChannelEn.mp3').then(function() {

        sayInChannel(connection, "mcdonald burger");

        // Create a new session
        const sessionClient = new dialogflow.SessionsClient();
        const sessionPath = sessionClient.sessionPath(projectId, sessionId);

        connection.on('speaking', (user, speaking) => {
          if (!speaking || user.bot) {
            return;
          }

          // this creates a 16-bit signed PCM, stereo 48KHz stream
          const audioStream = readable.createStream(user, {mode: 'pcm'});
          //Setting up detect stream.
          const detectStream = sessionClient
              .streamingDetectIntent()
              .on('error', console.error)
              .on('data', data => {
                if (!data.recognitionResult) {
                  parseTranscription(data.queryResult, msg, connection);
                }
              });

          // The first stream request to be sent, detailing
          // settings and parameters of the stream.
          const initialStreamRequest = {
            session: sessionPath,
            queryParams: {
              session: sessionClient.sessionPath(projectId, sessionId),
            },
            queryInput: {
              audioConfig: {
                audioEncoding: encoding,
                sampleRateHertz: sampleRateHertz,
                languageCode: languageCode,
              },
              singleUtterance: false,
            },
          };

          //converts Discord audio to 1 channel, and sends it through the detect stream to be transcribed.
          const convertTo1ChannelStream = new ConvertTo1ChannelStream();

          detectStream.write(initialStreamRequest);

          pump(
            audioStream.pipe(convertTo1ChannelStream),
            // Format the audio stream into the request format.
            through2.obj((obj, _, next) => {
              next(null, {
                inputAudio: obj
              });
            }),
            detectStream
          );

        });})
      }).catch(console.error);
  }
}

/**
 * Takes Dialogflow's transcription of the audio and figures out what it actually means.
 * (i.e. which champion it refers to, does the user want to start or check a timer, etc.)
 * Response comes in from Dialogflow as a semicolon-separated value, with format:
 * <type of response>;<champName>;<spellName>.
 * @param result the transcription of audio from Dialogflow
 * @param msg the original message telling the bot to start timing.
 */
function parseTranscription(result, msg, connection) {
  let query = result.queryText;
  let intent = result.intent;
  if (intent) {
    let response = result.fulfillmentText;
    console.log("query:" + query);
    console.log("response:" + response);

    let split = response.split(';');
    let type = split[0];
    let champName = split[1];
    let spellName = split[2];

    let getParams = {
      type: type,
      champName: champName,
      spellName: spellName,
      voiceChannel: msg.member.voice.channelID,
    };

    if (query === 'clear timers') {
      console.log('timers cleared');
      channelSpecificTimers.clear();
    }

    if (type === 'timer' || type === 'inquiry') {
      verifySpellAndChamp(getParams, msg, connection);
    } else {
      console.log("invalid speech");
    }
  } else {
    // msg.reply("Sorry, stopwatch couldn't understand your intent.");
  }

}

/**
 * Method that pings the Java backend to identify
 * which champion the user has spoken (and Dialogflow has transcribed).
 * @param getParams parameters to the request made to the backend
 * @param msg the original message sent to start timers.
 */
function verifySpellAndChamp(getParams, msg, connection) {
  let getUrl = "http://localhost:6789/";
  console.log("im here verifying");
  let reqClient = reqJSON.createClient(getUrl);
  reqClient.post("/nameValidator", getParams, function(err, res, body) {
    let champName = body.champName;
    let spellName = body.spellName;
    console.log("sent post");
    if (body.statusCode === 'success') {
      if (body.type === "timer") {
        let spellCooldown = body.spellCooldown;
        console.log("starting timer");
        startTimer(champName, spellCooldown, spellName, msg, connection);
      } else if (body.type === "inquiry") {
        //check timer map for inquiry
        console.log("inquiry pepe")
        checkTimer(champName, spellName, msg, connection);
      }
    } else if (body.statusCode === 'failure') {
        if (body.statusMsg === "Champion not found.") {
          console.log("Backend couldn't recognize champion.");
        } else if (body.statusMsg === "Spell not found.") {
          console.log("Backend couldn't recognize spell.")
        }
    }
  });
}

/**
 * Starts the timer for a specific champion's spell.
 * @param champName the name of the champion
 * @param spellCooldown full cooldown of the spell, in seconds
 * @param spellName the name of the spell
 * @param msg the original message
 */
function startTimer(champName, spellCooldown, spellName, msg, connection) {

  //first: find map containing timers belonging to this summoner.
  let timersForSummoner = channelSpecificTimers.get(msg.member.voice.channelID);

  if (timersForSummoner.has(champName)) {
    let champSpells = timersForSummoner.get(champName);
    if (champSpells.has(spellName)) {
      sayInChannel(connection, champName + " " + spellName + " timer already running.");
      return;
    } else {
      champSpells.set(spellName, spellCooldown);
      sayInChannel(connection, champName + " " + spellName + " timer started for " + spellCooldown + " seconds.");
    }
  } else {
    let spell = new Map();
    spell.set(spellName, spellCooldown);
    timersForSummoner.set(champName, spell);
    sayInChannel(connection, champName + " " + spellName + " timer started for " + spellCooldown + " seconds.");
  }

  let champSpells = timersForSummoner.get(champName);

  let timer = setInterval(function() {
    let specificCooldown = champSpells.get(spellName);
    specificCooldown -= 10;
    champSpells.set(spellName, specificCooldown);

    //if spell is now off cooldown
    if (specificCooldown <= 0) {
      sayInChannel(connection, champName + " has " + spellName + " now!");
      champSpells.delete(spellName);
      clearInterval(timer);
    }
  }, 1000);

}

/**
 * Checks the status of a champion's summoner spell. This will output a voice message
 * in the voice channel detailing how many seconds are left.
 * @param champName the name of the champion
 * @param spellName the name of the spell
 * @param msg the original message
 */
function checkTimer(champName, spellName, msg, connection) {
  //first: find map containing timers belonging to this summoner.
  let timersForSummoner = channelSpecificTimers.get(msg.member.voice.channelID);
  if (timersForSummoner.has(champName)) {
    let champSpells = timersForSummoner.get(champName);
    if (champSpells.has(spellName)) {
      //different variations of the message for grammar, lol
      let remainingCd = champSpells.get(spellName);
      if (remainingCd % 60 === 0) {
        sayInChannel(connection, champName + " has " + spellName + " in " + Math.floor(remainingCd/60) + " minutes.");
      } else {
        sayInChannel(connection, champName + " has " + spellName + " in " + Math.floor(remainingCd/60) + " minutes and " + remainingCd % 60 + " seconds.");
      }
    } else {
      sayInChannel(connection, champName + " has " + spellName + " up.");
    }
  } else {
    sayInChannel(connection, champName + " has " + spellName + " up.");
  }
}

/**
 * Function for a bot to speak some text in the voice channel.
 * @param channel the voice channel that the bot is in
 * @param text the text to be uttered
 */
async function saveTextAsWAV(text) {
  return new Promise((resolve, reject) => {
    say.export(text, 'Alex', 1, 'msg.wav', (err) => {
      if (err) {
        reject(err)
      }
      console.log ('Text saved.');
      resolve();
    });
  });
}

function sayInChannel(connection, text) {
  saveTextAsWAV(text).then(() => {
    playFile(connection, defaultMsgAudioPath);
  });
}
