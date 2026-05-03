FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
# Set on Railway (and docker-compose build args) so the SPA calls your API — must include /api
ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}
RUN npm run build

# Railway sets PORT; `serve` listens on that port (nginx-on-80 is awkward with Railway’s PORT)
FROM node:20-alpine
RUN npm install -g serve@14.2.4
WORKDIR /app
COPY --from=build /app/dist ./dist
ENV PORT=3000
EXPOSE 3000
CMD ["sh", "-c", "exec serve -s dist -l tcp://0.0.0.0:${PORT}"]
