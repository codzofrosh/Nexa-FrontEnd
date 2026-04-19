# Nexa Frontend

React + TypeScript SPA for the Nexa platform, built with Vite and served via Nginx in Docker. The image is published to GitHub Container Registry (GHCR) and deployed to EC2.

## Tech stack

- **React 18** with React Router v6
- **TypeScript 5** + Vite 5
- **Nginx 1.27** (Alpine) as the production server
- **Docker** multi-stage build (Node 20 builder → Nginx server)

## Local development

```bash
npm install
npm run dev        # dev server at http://localhost:5173
npm run build      # production build to /dist
npm run preview    # preview the production build locally
```

## Docker

### Build locally

```bash
docker build -t nexa-frontend .
docker run -p 80:80 nexa-frontend
```

### Pull from GHCR

```bash
docker pull ghcr.io/codzofrosh/nexa-frontend:latest
docker run -p 80:80 ghcr.io/codzofrosh/nexa-frontend:latest
```

## CI/CD — GitHub Actions

The workflow at [.github/workflows/docker-publish.yml](.github/workflows/docker-publish.yml) runs on every push to `main` and on manual trigger (`workflow_dispatch`).

**What it does:**
1. Checks out the repo
2. Logs in to GHCR using `GITHUB_TOKEN`
3. Builds the Docker image and pushes it to `ghcr.io/codzofrosh/nexa-frontend:latest`

No secrets need to be configured manually — `GITHUB_TOKEN` is provided automatically by GitHub Actions.

## Making the GHCR package public

After the first workflow run:

1. Go to **github.com/codzofrosh → Packages → nexa-frontend**
2. Click **Package settings → Change visibility → Public**

This allows the EC2 instance to pull the image without authentication.

## EC2 deployment

If the package is kept **private**, authenticate once on the EC2 instance:

```bash
echo $GITHUB_TOKEN | docker login ghcr.io -u codzofrosh --password-stdin
```

Then pull and run:

```bash
docker pull ghcr.io/codzofrosh/nexa-frontend:latest
docker run -d -p 80:80 --restart unless-stopped ghcr.io/codzofrosh/nexa-frontend:latest
```

## Project structure

```
├── src/                  # React application source
├── public/               # Static assets
├── Dockerfile            # Multi-stage build (Node → Nginx)
├── nginx.conf            # Nginx config (proxies /api/* to backend)
├── vite.config.ts
├── tsconfig.json
└── .github/
    └── workflows/
        └── docker-publish.yml   # CI/CD pipeline
```

## Backend integration

All `/api/*` requests are proxied to the backend by Nginx. The `VITE_API_URL` env var is intentionally left empty so all API calls go to the same origin, keeping CORS simple.

Backend repository: `https://github.com/codzofrosh/nexa-beeper-connector`
