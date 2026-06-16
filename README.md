# WHIMC-Observations

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/whimc/Observations?label=download&logo=github)](https://github.com/whimc/Observations/releases/latest)

Create holographic observations in Minecraft worlds using guided templates or freehand text. Observations are stored in MySQL and rendered as DecentHolograms holograms.

## Installation

1. Download the latest `WHIMC-Observations-*.jar` from the [releases page](https://github.com/whimc/Observations/releases/latest).
2. Place it in your server's `plugins/` folder.
3. Install the required dependencies (see below).
4. Configure `plugins/WHIMC-Observations/config.yml` with your MySQL credentials.
5. Restart the server.

## Dependencies

**Required** (must be installed on the server):

* [DecentHolograms](https://www.spigotmc.org/resources/decentholograms-1-8-1-21-5-papi-support-no-dependencies.96927/) — renders observation holograms
* [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) — powers the in-game sign editor for custom text input

**Optional:**

* [Multiverse-Core](https://www.spigotmc.org/resources/multiverse-core.390/) — soft dependency
* [Quests](https://www.spigotmc.org/resources/quests.3711/) — integration via custom objective (`loadbefore`)

## Building

Compile a JAR from the command line with Maven:

```
mvn clean package
```

The output appears in `target/` as `WHIMC-Observations-<version>.jar`.

Pushes to `master` automatically build and publish a GitHub release via Actions.

## Commands

### `/observe`

Root command for making observations.

| Command | Permission | Description |
|---|---|---|
| `/observe` | `whimc-observations.observe` | Open a GUI to form a templated observation |
| `/observe <observation>` | `whimc-observations.observe.freehand` | Write your own observation without a template |

Players with `whimc-observations.observe.customresponse` can also type custom fill-in responses during the template flow (via the sign editor).

### `/adminobservations`

Manage observations that have been created on the server.

| Command | Permission | Description |
|---|---|---|
| `/adminobservations` | `n/a` | List all `/adminobservations` subcommands |
| `/adminobservations info` | `whimc-observations.observations.info` | Show information about an observation |
| `/adminobservations list` | `whimc-observations.observations.list` | List all observations (or filter by player and/or world) |
| `/adminobservations near` | `whimc-observations.observations.near` | List all observations in a radius |
| `/adminobservations purge` | `whimc-observations.observations.purge` | Purge all temporary observations |
| `/adminobservations reactivate` | `whimc-observations.observations.reactivate` | Reactivate observations within a given time frame |
| `/adminobservations remove` | `whimc-observations.observations.remove` | Remove an observation |
| `/adminobservations removeall` | `whimc-observations.observations.removeall` | Remove all observations from a certain player and/or in a certain world |
| `/adminobservations setexpiration` | `whimc-observations.observations.setexpiration` | Change the expiration date of an observation |
| `/adminobservations teleport` | `whimc-observations.observations.teleport` | Teleport to an observation |

## Content moderation

Free-text observations are validated before they are placed. This applies to:

* `/observe <observation>` (freehand command)
* Uncategorized observations entered via the template GUI sign
* Custom fill-in responses typed through the sign editor during template selection

Preset template options chosen from clickable lists are not filtered.

### Profanity and slurs

Submissions containing blocked language are rejected. The player sees a message asking them to rephrase and try again.

Online server operators are notified in chat and the attempt is logged to the console with a censored preview of the text.

The blocklist ships in `blocked-words.txt` inside the plugin JAR (`src/main/resources/blocked-words.txt` in source). One term per line; lines starting with `#` are comments. Matching uses word boundaries and normalizes common leet-speak substitutions and stretched letters (e.g. `f@ck`, `fuuuuck`).

To customize the list, edit `blocked-words.txt` and rebuild the plugin.

### Nonsense / spam detection

Observations that look like keyboard spam are rejected. For example, holding down a single key (`aaaaaaaaaaaaaaaa`) or other very low-diversity text.

When using the sign editor, the menu reopens so the player can try again. For `/observe <text>`, the player receives a message asking for a meaningful observation.

## Config

### General

| Key | Type | Description |
|---|---|---|
| `debug` | `boolean` | Enable/disable debug messages (shows queries and other information in console) |
| `expiration-days` | `integer` | The amount of time (in days) before an observation expires |
| `enable-click-to-view` | `boolean` | Whether clicking an observation teleports you to the view point where it was created. Setting this to `false` also avoids players colliding with and bouncing off holograms |

#### Example

```yaml
debug: false
expiration-days: 7
enable-click-to-view: false
```

### MySQL

| Key | Type | Description |
|---|---|---|
| `mysql.host` | `string` | The host of the database |
| `mysql.port` | `integer` | The port of the database |
| `mysql.database` | `string` | The name of the database to use |
| `mysql.username` | `string` | Username for credentials |
| `mysql.password` | `string` | Password for credentials |

#### Example

```yaml
mysql:
  host: localhost
  port: 3306
  database: minecraft
  username: user
  password: pass
```

### Template GUI

| Key | Type | Description |
|---|---|---|
| `template-gui.text.write-your-own-response` | `string` | The fill-in option for writing your own response. Unless overridden, the highlight color of the category is automatically applied |
| `template-gui.text.custom-response-sign-header` | `string` | The sign header when entering a custom response |
| `template-gui.text.uncategorized-sign-header` | `string` | The sign header when making an uncategorized observation |
| `template-gui.filler-item` | `Minecraft material` | The item to use for filler spaces in the GUI |
| `template-gui.inventory-name` | `string` | The name of the inventory used for the GUI |
| `template-gui.rows` | `integer` | The number of rows that will be in the GUI (range 1–6) |
| `template-gui.cancel.enabled` | `boolean` _(optional, default `true`)_ | Whether to show the cancel button |
| `template-gui.cancel.item` | `Minecraft material` | The item to use for the cancel button |
| `template-gui.cancel.position` | `integer` | The position of the cancel button |
| `template-gui.cancel.name` | `string` | The text to display for the cancel button |
| `template-gui.cancel.lore` | `string list` | The lore of the cancel button |
| `template-gui.uncategorized.enabled` | `boolean` _(optional, default `true`)_ | Whether to show the uncategorized observation button |
| `template-gui.uncategorized.item` | `Minecraft material` | The item to use for the uncategorized observation button |
| `template-gui.uncategorized.position` | `integer` | The position of the uncategorized button |
| `template-gui.uncategorized.name` | `string` | The text to display for the uncategorized button |
| `template-gui.uncategorized.lore` | `string list` | The lore of the uncategorized button |

#### Example

```yaml
template-gui:
  text:
    write-your-own-response: "Write your own response"
    custom-response-sign-header: "&f&nYour Response"
    uncategorized-sign-header: "&f&nYour Observation"
  filler-item: white_stained_glass_pane
  inventory-name: "&lChoose an observation type!"
  rows: 4
  cancel:
    enabled: true
    item: barrier
    position: 27
    name: "&cCancel"
    lore:
      - "&7Cancel your observation"
  uncategorized:
    enabled: true
    item: oak_sign
    position: 35
    name: '&7Uncategorized Observation'
    lore:
      - '&fMake a custom observation without a template.'
      - ''
      - '&8&oThis can also be done with &7&o/observe <observation>'
```

### Templates

There are 5 observation categories supported by the plugin: `ANALOGY`, `COMPARATIVE`, `DESCRIPTIVE`, `INFERENCE`, and `QUESTION`

| Key | Type | Description |
|---|---|---|
| `templates.<category>.gui.enabled` | `boolean` _(optional, default `true`)_ | Whether to show this item in the GUI |
| `templates.<category>.gui.item` | `Minecraft material` | The item to use for this category in the GUI |
| `templates.<category>.gui.position` | `integer` | The position of the category's item |
| `templates.<category>.gui.name` | `string` | The name of the category's item |
| `templates.<category>.gui.lore` | `string list` | The lore of the category's item |
| `templates.<category>.prompts` | `prompt list` | A list of prompts |

Each prompt has the prompt itself and a list of world-specific fill-ins for the prompt.
Within the `prompt`, a fill-in is notated via `{}`. A prompt can have as many fill-ins as you'd like.
You'll use the index of each `{}` to configure the fill-ins for that prompt in a given world.

#### Example prompt

```yaml
- prompt: The {} is {}.
  worlds:
    world1: # These options will only be shown for players in world1
      0: # These will be the fill-ins for the first {}
      - moon
      - water level
      1: # These will be the fill-ins for the second {}
      - large
      - missing
      - low
      - high
    world2: # These options will only be shown for players in world2
      # ...
```

#### Example template

```yaml
templates:
  DESCRIPTIVE:
    gui:
      enabled: true
      item: orange_concrete
      position: 13
      name: '&6Descriptive Observation'
      lore:
        - '&fRelated to color, temperature, quantity, and other physical attributes.'
        - ''
        - '&7Example:'
        - '  &f&o There are lots of trees.'
    prompts:
      - prompt: There are lots of &7{}&r.
        worlds:
          world:
            0:
              - coral
              - trees
      - prompt: There are no &7{}&r.
        worlds:
          world:
            0:
              - animals
              - plants
      - prompt: It is really &7{}&r.
        worlds:
          world:
            0:
              - hot
              - cold
      - prompt: The &7{}&r is &7{}&r.
        worlds:
          world:
            0:
              - moon
              - water level
            1:
              - large
              - missing
              - low
              - high
```
