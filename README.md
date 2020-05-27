# Stopwatch
Stopwatch is a Discord bot to help you in the game, League of Legends. When you're in a voice channel with your friends and in an active game, Stopwatch will sit in the same channel as you and detect whenever you say that a champion summoner spell has been used. The bot keeps track of the cooldown of the spell, notifying when it is ready to be used again. Additionally, you can also ask the bot if a certain champion has some summoner spell active, and it'll respond accordingly!

## Getting Started


### Prerequisites

You'll need to have the Discord app as well as development keys for the following:
* Discord 
* Riot API
* Dialogflow

The bot uses node.js to interact with Discord and Java to interact with the Riot API. For a full list of the dependencies, check ```package.json```. Stopwatch also uses Maven as a build automation tool.

### Installing

Clone the repository into the working directory of your choosing. From there, place your keys into a .env file in the StopwatchBot directory. Your env file should contain the following:

```
DISCORD_KEY=<insert Discord bot key>
API_KEY=<insert Riot API key>
GOOGLE_APPLICATION_CREDENTIALS="<insert file path to Google credentials JSON>"

```

## Deployment

To run the program, first navigate to the StopwatchBot directory and type

```mvn package; ./run```

in your terminal. This will start the Java backend portion of the bot. To start the bot itself and interact with it in Discord, using a separate terminal, type the command 

```node bot.js```

## Using the Bot
Once the bot is up and running, you can use it to time summoner spells for #Summoner's Rift games only#. To do so, join a voice channel in your server and type the command 

```!timer <summoner name>```

in order to have the bot join your channel and start timing. The bot will record timers of champions on the enemy team, so the enemy team of the summoner who you typed in. 

The bot detects certain keyphrases to start a timer. These take the form 

```<champion name> has no <spell>```
```<champion name> no <spell>```
```<champion name> <spell>```

To ask the bot if a certain spell is off cooldown or not, say:

```Does <champion name> have <spell>?```


## Acknowledgments

* Huge thanks to [this article](https://refruity.xyz/writing-discord-bot/) which helped me get a lot of the voice functionality up and running! It ended up being pretty difficult to actually get the voice data from Discord to Dialogflow, and this article gave me a good start.
