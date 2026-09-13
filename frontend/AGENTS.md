# AGENTS.md

## Dev Commands
- `bun run dev` — dev server (proxies `/api` → `localhost:8080`, `/mcp` → `localhost:8001`, `/ws` → `ws://localhost:8080`)
- `bun run build` — runs `tsc -b && vite build` (typecheck before build)
- `bun run lint` — ESLint
- `bun run preview` — preview build

## Setup
- **bun is required** (`preinstall` enforces it, engine check `>=1.0`)
- Copy `.env.template` to `.env` and fill `VITE_BACK_END` and `VITE_MCP_SERVER`

## Tech Stack & Config
- React 19 + TypeScript 5 + Vite 7 + Tailwind CSS 4 + Ant Design 6 + Zustand
- **React Compiler is active** (`babel-plugin-react-compiler` / `eslint-plugin-react-compiler`) — do not disable or remove
- TypeScript: `verbatimModuleSyntax`, `erasableSyntaxOnly`, strict mode enabled
- Monaco editor pre-loaded at app startup (`src/main.tsx`)

## Architecture
- Single-page app with client-side routing (`react-router-dom`)
- Pages: Home, Document (list/directory/content), Project (list/:id), Code (sf/:id, cp/:id, /), User, Login, Signin
- Ant Design theme: teal primary (`#13c2c2`)

## Build Output
- Manual chunk splitting via `vite.config.ts` — ui-vendor, icon-vendor, editor-vendor, router-state, markdown-vendor, util-vendor
- `chunkSizeWarningLimit: 4000` (monaco-editor is large, intentionally bumped)
