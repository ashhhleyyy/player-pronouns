# Player Pronouns
Let players share their pronouns!

## For players

### Commands
To change your displayed pronouns, you can use the command `/pronouns`.
It will suggest pronouns that are configured by the server admins, along with the default set. By default, you do not have to pick one of the suggestions at all, however server owners may disable setting custom pronouns in case of abuse, although it is not recommended to do so permanently.

## For server owners

### Configuration
The mod should work out of the box without any configuration, however if you want player's pronouns to be visible, you probably want to use the placeholder somewhere.

#### Adding custom pronouns (eg. neo-pronouns)
To add custom pronoun sets, you can use the `single` and `pairs` options in the config file. `single` is for singular options such as `any` or `ask` while `pairs` is for pronouns that come in pairs and are used in the form `a/b`, for example `they` and `them`.

#### Displaying pronouns

##### In chat with Styled Chat
[Styled Chat](https://modrinth.com/mod/styled-chat) allows you to customise the formatting of chat messages.
To configure pronouns to show up like this, you can set the `chat` style to the following:

`<${player} [%playerpronouns:pronouns%]> ${message}`

![](https://cdn.discordapp.com/attachments/859419898962116642/870732808367267881/in-chat.png)

##### On the tab list with Styled Player List
[Styled Player List](https://modrinth.com/mod/styledplayerlist) allows you to customise the look and feel of the tab/player list, as well as customise the formatting used for players in the list.

```json
{
  "_comment": "Ensure that you include all the other default config options",
  "changePlayerName": true,
  "playerNameFormat": "%player:displayname% (%playerpronouns:pronouns%)",
  "updatePlayerNameEveryChatMessage": true
}
```

![](https://cdn.discordapp.com/attachments/859419898962116642/870739744286453820/2021-07-30_19.45.49.png)
