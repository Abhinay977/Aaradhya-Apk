import asyncio
import websockets
import json
import os

async def test_ws():
    # Use real key if you have one, or FAKE_KEY
    url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=FAKE_KEY"
    try:
        async with websockets.connect(url) as ws:
            setup_msg = {
                "setup": {
                    "model": "models/gemini-2.0-flash-exp",
                    "systemInstruction": {
                        "parts": [
                            {"text": "Hello world"}
                        ]
                    },
                    "generationConfig": {
                        "responseModalities": ["AUDIO"],
                        "speechConfig": {
                            "voiceConfig": {
                                "prebuiltVoiceConfig": {
                                    "voiceName": "Aoede"
                                }
                            }
                        },
                        "temperature": 0.9
                    }
                }
            }
            print("Connected! Sending setup message...")
            await ws.send(json.dumps(setup_msg))
            print("Sent! Waiting for response...")
            msg = await ws.recv()
            print(f"Received: {msg}")
    except Exception as e:
        print(f"Failed: {e}")

asyncio.run(test_ws())
