#!/bin/zsh

# Function to display messages
echo_info() {
  echo -e "\e[34m[INFO]\e[0m $1"
}

echo_error() {
  echo -e "\e[31m[ERROR]\e[0m $1"
}

# Step 1: Build the Maven project
echo_info "Building the Maven project..."
mvn clean verify
if [ $? -ne 0 ]; then
  echo_error "Maven build failed."
  exit 1
fi

# Step 2: Stop and remove existing Docker container (if running)
echo_info "Stopping and removing any existing Docker container 'bills-app'..."
docker stop bills-app 2>/dev/null || echo_info "No existing container to stop."
docker container rm bills-app 2>/dev/null || echo_info "No existing container to remove."

# Step 3: Build the Docker image
echo_info "Building the Docker image for 'bills-app'..."
docker build --no-cache -t bills-app .
if [ $? -ne 0 ]; then
  echo_error "Docker build failed."
  exit 1
fi

# Step 4: Run the Docker container
echo_info "Running the Docker container 'bills-app'..."
docker run -d \
  -p 8080:8080 \
  --name bills-app \
  --memory="512m" \
  --cpus="0.5" \
  --network bills-net \
  -e DB_URL="jdbc:mysql://mysql-bills:3306/bills" \
  -e SPRING_DATASOURCE_USERNAME="${DB_USERNAME:-root}" \
  -e SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD:-password}" \
  -e SPRING_PROFILES_ACTIVE=dev \
  bills-app \
  java -Xmx256m -Xms128m -jar app.jar

# Check if the container started successfully
if [ $? -eq 0 ]; then
  echo_info "Docker container 'bills-app' is now running."
else
  echo_error "Failed to start Docker container 'bills-app'."
  exit 1
fi

