# ================================================
# Stage 1: Build the application
# ================================================
FROM node:22.13-alpine AS build

# Set working directory
WORKDIR /usr/src/app

# Copy package files from the app folder
COPY app/package*.json ./
# (Optional) If you run gradle tasks inside Docker, you could copy build.gradle.kts too
# COPY app/build.gradle.kts ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY app/ ./

# (Optional) If you had a build step for a front-end, you might do: 
# RUN npm run build

# ================================================
# Stage 2: Create the final image
# ================================================
FROM node:22.13-alpine AS build-final

# Set working directory
WORKDIR /usr/src/app

# Copy files from the build stage
COPY --from=build /usr/src/app /usr/src/app

# Expose the backend port
EXPOSE 3000

# Start the application
CMD ["node", "index.js"]