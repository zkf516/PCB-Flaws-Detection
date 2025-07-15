## Test Commands

### 1. Inference
```bash
curl -X POST -F "file=@uploads/20250712-094718.jpg" http://localhost:5001/api/v1/inference
```

```bash
# Test with another image
curl -X POST -F "file=@uploads/20250712-094833.jpg" http://localhost:5001/api/v1/inference

# Test with a third image
curl -X POST -F "file=@uploads/20250712-095150.jpg" http://localhost:5001/api/v1/inference
```

### 2. History
```bash
# Get all inference histories (no date filter)
curl -X GET http://localhost:5001/api/v1/get_all_histories

# Get all histories with pagination
curl -X GET "http://localhost:5001/api/v1/get_all_histories?page=1&limit=10"

# Get histories filtered by date (format: YYYYMMDD)
curl -X GET "http://localhost:5001/api/v1/get_all_histories?date=20250714"

# Get specific history result (replace with actual image_id)
curl -X GET "http://localhost:5001/api/v1/get_one_history?image_id=20250714-181326.jpg"
```

### 3. Access marked images
After running inference, the marked images are saved in the uploads folder with bounding boxes drawn on them.

```bash
# View the marked image in browser
http://localhost:5001/images/20250714-181326.jpg
```

### 4. Debug database issues
```bash
# Check database connection by running inference first, then check histories
curl -X POST -F "file=@uploads/20250712-094718.jpg" http://localhost:5001/api/v1/inference
curl -X GET http://localhost:5001/api/v1/get_all_histories
```
---
not tested:
### 5. IoT Device APIs (Port 5001)
```bash
# Get all IoT devices
curl -X GET http://localhost:5001/api/v1/get_all_devices

# Get specific device shadow data (replace with actual device_id)
curl -X GET "http://localhost:5001/api/v1/get_device_shadow?device_id=your_device_id"
```

### 6. WebSocket Server (Port 5002)
```bash
# Connect to WebSocket for real-time IoT device data
# Use a WebSocket client like wscat, websocat, or browser console

# Install wscat if not available:
# npm install -g wscat

# Connect to device stream (replace 'device_id' with actual device ID)
wscat -c ws://localhost:5002/device_id

# Example with specific device ID:
wscat -c ws://localhost:5002/your_device_id

# Using websocat (alternative WebSocket client):
# websocat ws://localhost:5002/device_id

# JavaScript in browser console:
# const ws = new WebSocket('ws://localhost:5002/device_id');
# ws.onmessage = function(event) { console.log('Received:', event.data); };
# ws.onopen = function(event) { console.log('Connected to WebSocket'); };
# ws.onerror = function(error) { console.log('WebSocket Error:', error); };
```

### 7. Test WebSocket with curl alternatives
```bash
# Test WebSocket connection using curl (limited functionality)
# Note: curl doesn't fully support WebSocket protocol, use wscat instead

# For testing purposes, you can verify the server is running:
nc -zv localhost 5002

# Check if the WebSocket server is responding (this will fail with protocol error but confirms server is up):
curl -v -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==" -H "Sec-WebSocket-Version: 13" http://localhost:5002/device_id
```

