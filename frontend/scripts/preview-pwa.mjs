import { createServer } from 'node:http'
import { readFile, stat } from 'node:fs/promises'
import { resolve, extname, sep } from 'node:path'
const root = resolve('.output/public')
const port = Number(process.env.PORT || 4173)
const mime = { '.html': 'text/html', '.js': 'text/javascript', '.css': 'text/css', '.json': 'application/json', '.webmanifest': 'application/manifest+json', '.svg': 'image/svg+xml', '.png': 'image/png', '.woff2': 'font/woff2', '.woff': 'font/woff', '.ico': 'image/x-icon' }
createServer(async (req, res) => {
  try {
    const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname)
    let file = resolve(root, '.' + pathname)
    if (file !== root && !file.startsWith(root + sep)) { res.writeHead(403).end(); return }
    if ((await stat(file).catch(() => null))?.isDirectory()) file = resolve(file, 'index.html')
    let data = await readFile(file).catch(() => null)
    if (!data && !extname(pathname) && !pathname.startsWith('/api/')) {
      file = resolve(root, 'index.html'); data = await readFile(file)
    }
    if (!data) { res.writeHead(404).end('Not found'); return }
    res.writeHead(200, { 'Content-Type': mime[extname(file)] || 'application/octet-stream', 'Cache-Control': 'no-cache' })
    res.end(data)
  } catch { res.writeHead(500).end('Preview failed. Build the frontend first.') }
}).listen(port, '127.0.0.1', () => console.log(`Boardwise preview: http://localhost:${port}`))
