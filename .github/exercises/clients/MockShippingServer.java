package dev.mikoto2000.springboot.shipping;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

final class MockShippingServer implements AutoCloseable {
  private final HttpServer server;
  private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
  private final ConcurrentHashMap<String, AtomicInteger> calls = new ConcurrentHashMap<>();

  MockShippingServer() throws IOException {
    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    server.setExecutor(executor);
    server.createContext("/rates/", exchange -> {
      try (exchange) {
        String area = exchange.getRequestURI().getPath().substring("/rates/".length());
        int call = calls.computeIfAbsent(area, key -> new AtomicInteger()).incrementAndGet();
        int status = switch (area) {
          case "unknown" -> 404;
          case "maintenance" -> 500;
          case "flaky" -> call <= 2 ? 503 : 200;
          case "empty" -> 204;
          default -> 200;
        };
        if (area.equals("slow")) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return;
          }
        }
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] body = ("{\"area\":\"" + area + "\",\"feeYen\":600}")
            .getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, status == 204 ? -1 : body.length);
        if (status != 204) {
          exchange.getResponseBody().write(body);
        }
      } catch (IOException clientDisconnected) {
        // The timeout test intentionally closes its connection before the response.
      }
    });
    server.start();
  }

  String baseUrl() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  int calls(String area) {
    AtomicInteger count = calls.get(area);
    return count == null ? 0 : count.get();
  }

  void reset() {
    calls.clear();
  }

  @Override
  public void close() {
    server.stop(0);
    executor.shutdownNow();
  }
}
