# Internal APIs & Protocols (Standalone Architecture)

| API Type | Path / Function | Purpose | Method | Details |
|---|---|---|---|---|
| Python Local Bridge | `get_metadata(video_id)` | OEmbed fetch via HTTP (bypass Youtube JS parser) | Synchronous call to Chaquopy Runtime | Returns title, author & thumbnails. |
| Python Local Bridge | `get_transcript(video_id)` | Extract transcripts directly from youtube metadata using IP | Synchronous call to Chaquopy Runtime | Returns string value up to length of video in full text formatting without timestamps. |
| Google REST | `v1beta/models/[MODEL]:generateContent` | Sent directly from client side wrapper | OkHttp POST, x-goog-api-key Authentication Header | Sends request payload including SafetySettings, GenerationConfig and **thinkingConfig** (thinkingBudget=0) tailored specifically for Android App. Receives `AiResult.Success` or HTTP Error variants back to App Context. |
