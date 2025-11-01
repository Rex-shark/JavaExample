# game-demo1

Simple Bootstrap 5 RPG move demo served by Node.js 22 and containerized with Docker.

## Run locally (Windows cmd)

```
cd D:\idea\IdeaProjects\JavaExample\game-demo1
npm install
npm start
```

Open http://localhost:3000

Controls: Arrow keys (or WASD) to move the character on a 16x16 tile map.

## Build and run with Docker (Node 22)

```
cd D:\idea\IdeaProjects\JavaExample\game-demo1
docker build -t game-demo1:latest .
docker run --rm -p 3000:3000 --name game-demo1 game-demo1:latest
```

Then open http://localhost:3000

## Notes
- The app serves static files from the module root; `index.html` includes `./js/game.js` and `./css/bootstrap.min.css`.
- You can adjust map size or tile size in `js/game.js` (TILES and TILE_SIZE constants).

