from http.server import SimpleHTTPRequestHandler, HTTPServer

PORT = 8000

class MyHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Cache-Control', 'no-store, no-cache, must-revalidate, max-age=0')
        return super().end_headers()

if __name__ == "__main__":
    print(f"Serving on http://localhost:{PORT}")
    server = HTTPServer(("localhost", PORT), MyHandler)
    server.serve_forever()