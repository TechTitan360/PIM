FROM oven/bun:1
WORKDIR /app

# Copy backend folder contents
COPY backend/package.json backend/bun.lock ./
RUN bun install

COPY backend/ .

EXPOSE 3000
CMD ["bun", "run", "src/index.ts"]
