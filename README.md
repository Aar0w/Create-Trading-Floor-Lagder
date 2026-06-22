# Trading Floor Ledger

An addon for Create: Trading Floor — tracks how many trades each player has completed through Trading Depots and fires a Forge event for integrations.

## Installation

1. Put the mod in `mods/` (server-side only, no client install needed)
2. For integrations — install KubeJS + EventJS

## Command

```
/tf_trades
```
Shows your current trade count.

## How It Works

Trades are attributed to the player who **placed** the Trading Depot block — not whoever is standing nearby. The count is stored on the server's disk and persists even if the player is offline.

On every trade, the mod fires a Forge event:

```
com.cak.tradingfloor.fix.TradingTradeEvent
```

Event fields:
| Field | Type | Description |
|---|---|---|
| `player` | `ServerPlayer` | the player who owns the depot |
| `totalTrades` | `int` | the player's total recorded trade count |
| `depotPos` | `BlockPos` (nullable) | depot position; `null` if the event was fired on login for an offline player |

## KubeJS Integration

Listen to the event (requires **EventJS**):

```js
NativeEvents.onEvent(
    Java.loadClass('com.cak.tradingfloor.fix.TradingTradeEvent'),
    event => {
        const player = event.player;
        const total = event.totalTrades;

        // your logic — quests, rewards, achievements, leaderboards
    }
);
```

Get any player's trade count directly via Java (e.g. for a leaderboard):

```js
const TradeEventBridge = Java.loadClass('com.cak.tradingfloor.fix.TradeEventBridge');
const count = TradeEventBridge.getTradeCount(player.server, player.uuid);
```

### Example: FTB Quests milestone integration

```js
// kubejs/server_scripts/trading_floor_quests.js

const MILESTONES = [
    [5,   '57F4FE71F78DFA05'],
    [100, '33FCBCE23CA8214A'],
    [500, '2372061A10003C77'],
];

NativeEvents.onEvent(
    Java.loadClass('com.cak.tradingfloor.fix.TradingTradeEvent'),
    event => {
        const player = event.player;
        const total = event.totalTrades;

        for (const [required, taskId] of MILESTONES) {
            if (total === required) {
                player.server.runCommandSilent(
                    `ftbquests change_progress ${player.name.string} complete ${taskId}`
                );
            }
        }
    }
);
```

## Dependencies

- **Required:** Forge 1.20.1, Create: Trading Floor
- **Optional:** KubeJS, EventJS (for integrations), FTB Quests (for the example above)
