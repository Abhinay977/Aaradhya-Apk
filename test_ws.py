import asyncio
import websockets
import json

async def test_ws():
    url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=FAKE_KEY"
    try:
        async with websockets.connect(url) as ws:
            print("Connected! Sending perfect setup message...")
            await ws.send(json.dumps({
                "setup": {
                    "model": "models/gemini-2.0-flash-exp"
                }
            }))
            print("Sent! Waiting for response...")
            msg = await ws.recv()
            print(f"Received: {msg}")
    except Exception as e:
        print(f"Failed: {e}")

asyncio.run(test_ws())
