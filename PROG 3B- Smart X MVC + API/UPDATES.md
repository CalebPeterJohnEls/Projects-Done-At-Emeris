# Lecturer Feedback and Updates

This ledger records the feedback items currently identified for Parts 1 and 2, the implementation changes made in response, the source files involved, and practical steps for verifying each change.

| Part | Lecturer comment / feedback | What changed | Files changed | How to check |
|---|---|---|---|---|
| Part 1 | Use live data for the sensors instead of normal dummy data. | Added the `LiveDataSimulation` console application. It retrieves registered sensors from the API and continuously posts generated temperature, soil-moisture and power readings. The Sensors dashboard retrieves telemetry snapshots periodically, and the Command Stream page polls the API to display incoming readings. | `Smart_X_PROG/LiveDataSimulation/Services/ApiClient.cs`; `Smart_X_PROG/LiveDataSimulation/Services/DeviceRunner.cs`; `Smart_X_PROG/LiveDataSimulation/Program.cs`; `Smart_X_PROG/Smart_X_Client/Services/ApiService.cs`; `Smart_X_PROG/Smart_X_Client/Views/Sensors/Index.cshtml`; `Smart_X_PROG/Smart_X_Client/Controllers/CommandStreamController.cs`; `Smart_X_PROG/Smart_X_Client/Views/CommandStream/Index.cshtml` | Start the API and client, register or use an active sensor, then start `LiveDataSimulation`. Open the Sensors page and confirm that telemetry values refresh and change. Open Command Stream and confirm that new readings appear without manually refreshing the page. |
| Part 1 | Media uploading was a generic upload. | Updated the upload implementation to associate attachments with individual sensors, validate and save uploaded files in sensor-specific folders, and retain the files on disk. The sensor Details page allows additional attachments to be uploaded and displays image attachments as thumbnails. | `Smart_X_PROG/Smart_X_PROG/Controllers/FileController.cs`; `Smart_X_PROG/Smart_X_PROG/Services/FileService.cs`; `Smart_X_PROG/Smart_X_PROG/Controllers/SensorsController.cs`; `Smart_X_PROG/Smart_X_Client/Services/ApiService.cs`; `Smart_X_PROG/Smart_X_Client/Views/Sensors/Details.cshtml`; `Smart_X_PROG/docker-compose.yml` | Open a sensor's Details page, upload an allowed image or document, and confirm that it appears under that sensor. For an image, confirm that a thumbnail is displayed. Restart the application and confirm that the attachment is still available. In Docker, confirm persistence after stopping and starting the containers without deleting the volumes. |
| Part 2 | Queues and suggested actions needed more detailed pop-ups so users understand what is happening. | Added Bootstrap modal dialogs to the Command Stream page. Selecting each queue tile (standard, priority, processed and duplicates) opens a detail view of the relevant readings. The suggested-action “Why?” controls open a modal explaining the readings and the reason for the recommendation. | `Smart_X_PROG/Smart_X_Client/Views/CommandStream/Index.cshtml`; `Smart_X_PROG/Smart_X_Client/Controllers/CommandStreamController.cs`; `Smart_X_PROG/Smart_X_Client/Models/StreamModels.cs`; `Smart_X_PROG/Smart_X_PROG/Controllers/StreamController.cs`; `Smart_X_PROG/Smart_X_PROG/Services/StreamService.cs` | Open Command Stream while telemetry is available. Select each of the four queue tiles and confirm that its modal shows the corresponding details. Select the “Why?” control for a suggested action and confirm that the modal explains the underlying readings and 

## Screenshot Evidence

The following screenshots provide evidence of the implemented improvements.

### 1. Predictive Action and Suggestion Engine

**Screenshot 1: Suggested Actions / Recommendations**

![Predictive Action and Suggestion Engine](https://github.com/user-attachments/assets/7aeae62d-2f9b-4209-9e6f-30429c78f3de)

---

### 2. App Functionality: Sensor Telemetry Data Ingestion and UI

**Screenshot 2: Sensor Telemetry Data Ingestion**

![Sensor Telemetry Ingestion](https://github.com/user-attachments/assets/5c0f6464-a2c3-4398-823f-7cc08db9a61a)

---

### 3. Media / Log File Upload Attachment

**Screenshot 3: Media / Log File Selection and Upload**

![Media and Log File Upload](https://github.com/user-attachments/assets/931bd275-5350-4093-830f-ad8f19a69153)
---


