# Video Upload API Guide for Frontend

This guide explains how to upload videos to the backend, which stores them in AWS S3 and saves metadata in the database.

## Endpoint Details

- **URL**: `/api/videos/upload`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`

## Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `file` | File (Binary) | Yes | The video file to be uploaded. |
| `title` | String | Yes | The title of the video. |
| `description` | String | No | The description of the video. |

## Constraints

- **Max File Size**: 100 MB
- **Allowed Formats**: 
  - `video/mp4` (.mp4)
  - `video/quicktime` (.mov)
  - `video/x-msvideo` (.avi)
  - `video/x-matroska` (.mkv)
  - `video/webm` (.webm)

## Response Format

### Success (200 OK)

```json
{
  "success": true,
  "message": "Video subido exitosamente",
  "video": {
    "id": 1,
    "title": "Rutina de Piernas",
    "description": "Rutina intensa para cuadriceps",
    "s3Key": "videos/550e8400-e29b-41d4-a716-446655440000.mp4",
    "fileName": "my_workout.mp4",
    "contentType": "video/mp4",
    "size": 15482000,
    "createdAt": "2023-10-27T10:00:00",
    "updatedAt": "2023-10-27T10:00:00"
  }
}
```

### Error (400 Bad Request)
Occurs if the file is too large, wrong format, empty, or missing title.

```json
{
  "success": false,
  "message": "El archivo excede el tamaño máximo permitido: 100 MB"
}
```

## Update Video Details (PUT)

Updates the title and/or description of an existing video.

- **URL**: `/api/videos/{id}`
- **Method**: `PUT`
- **Content-Type**: `application/json`

**Request Body:**
```json
{
  "title": "Nuevo Título",
  "description": "Nueva descripción actualizada"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Video actualizado exitosamente",
  "video": {
    "id": 1,
    "title": "Nuevo Título",
    "description": "Nueva descripción actualizada",
    ...
  }
}
```

## Listing Videos (GET)

- **URL**: `/api/videos`
- **Method**: `GET`
- **Response**:

```json
{
  "success": true,
  "count": 5,
  "videos": [
    {
      "id": 1,
      "title": "Rutina de Piernas",
      "description": "Rutina intensa...",
      "key": "videos/...",
      "url": "https://s3-bucket-url...?signature=...",
      "fileName": "video.mp4",
      "size": 123456,
      "contentType": "video/mp4"
    },
    ...
  ]
}
```

## Deleting Videos (DELETE)

Deletes a video from both the database and AWS S3 storage.

- **URL**: `/api/videos/{id}`
- **Method**: `DELETE`

> **Note**: You must use the database numerical **ID**, NOT the S3 key.

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Video eliminado exitosamente",
  "id": 1
}
```

## Code Examples

### JavaScript (Fetch API - Upload)

```javascript
async function uploadVideo(videoFile, videoTitle, videoDescription) {
  const formData = new FormData();
  formData.append('file', videoFile);
  formData.append('title', videoTitle);
  if (videoDescription) {
    formData.append('description', videoDescription);
  }

  try {
    const response = await fetch('/api/videos/upload', {
      method: 'POST',
      body: formData,
    });
// ... rest of the code
```
      // Note: Do NOT set Content-Type header manually for FormData
    });

    const result = await response.json();
    
    if (response.ok) {
      console.log('Upload successful:', result.video);
      return result;
    } else {
      console.error('Upload failed:', result.message);
    }
  } catch (error) {
    console.error('Network error:', error);
  }
}
```
