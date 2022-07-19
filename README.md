# VALORANT IN GAME TRANSLATOR
This is a valorant in game chat translator written in java.

# HOW TO USE
before downloading and running the program you have to fill up the parameters that it comes with.
this will be a txt file named parameters.txt
EVERY SINGLE PARAMETER MUST BE FILLED.

# PARAMETERS EXPLAINED

## 1.excludeHost

set this to True (excludeHost:true) if you do not want your messages processed and sent back.

set this to False (excludeHost:false) if you want your messages processed translated and sent back to team/all chat.

this process is not automatic and requires a specific command. you have to type TR/send/{team/all}/{language}/{text you want to send}

EXAMPLE: TR/send/team/german/hello 
this will translate hello to german and send it in team chat

EXAMPLE: TR/send/all/french/hello 
this will translate hello to french and send it in all chat

NOTE:these commands are only for the host. other team members will not be able to use this command.

## 2.translateTo

what language you want foreign texts to be translated to.

EXAMPLE: translateTo:english

this will translate every NON native language to english and send it back to team chat.

## 3.nativeLanguage

your native language. obviously you don't want your own language to be translated duh.

EXAMPLE: nativeLanguage:english

this will ensure that if the text is english it will not be processed.

## 4.lockFileUrl

Path to lockFile. this is a temporary file that valorant creates when its launched. it contains all the necessary information needed to 
access chats and send texts.

LOCATION: "C:\Users\{user}\AppData\Local\Riot Games\Riot Client\Config\lockfile.file" this path is generified.
it is located in LocalAppData->riotClient->Config folder

do not worry this program does not save any chat logs nor send them anywhere because i value privacy and plus if valorant finds out that it does anything fishy
they'll yeet this program into void. it is open source so if you still have doubts you can just check the code.


## PROFANITY FILTER

There is a profanity filter but it only works for:
ENGLISH
SPANISH
GERMAN
FRENCH
RUSSIAN
UKRANIAN
POLISH

IF you are translating foreign languages to these listed ones there is a high chance the program will detect profanity and filter it.
IF not the beware because the program will translate the whole thing and include the profanity words as well so you might get reported for that.

------------------------------------------------------------------

thats pretty much it. make sure to open valorant first and THEN the translator. 

happy rank up :)


# CREDITS
[VALORANT-API-DOCS](https://github.com/techchrism/valorant-api-docs/tree/trunk/docs)
[TRANSLATION-API](https://github.com/nidhaloff/deep-translator)
[PROFANITY-FILTER](https://github.com/Twinkle942910/CheckMateFilter/tree/v1.0)
