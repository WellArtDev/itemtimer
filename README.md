# ItemTimer

Never lose loot to a silent despawn again. Dropped items vanish exactly
5 minutes after they hit the ground — ItemTimer draws a small,
nametag-style countdown (`4:32`) floating above every dropped item, so
you can see at a glance which stack is about to disappear and rescue it
first.

## Features

- **Live despawn countdown** — every dropped item shows its remaining
  time as `m:ss`, updating every frame
- **Red warning** — the timer turns red when under 60 seconds remain,
  so urgent loot stands out
- **Nametag style** — camera-facing, nametag-sized text that floats
  above each item, full-bright so it stays readable in the dark
- **Zero config** — drop it in and play

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/) 0.19.2+ for
   Minecraft 26.2
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) for
   Minecraft 26.2
3. Drop `itemtimer-1.0.0.jar` into your `mods` folder
4. Launch and play — no config needed

## Notes

- Client-side only. Works on any server (including vanilla).
- No mixins, no networking, nothing saved to disk.
- Items that never despawn (e.g. from creative `/give` with age reset
  mechanics) count down from the same 5-minute window the game uses.

## License

MIT — see [LICENSE](LICENSE).
