package com.muscat.Collabus.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 대상 서버 앞에서 바이트를 그대로 넘기는 TCP 중계기. 기존 연결만 소리 없이 끊긴 상태를 흉내 낸다.
 */
public final class BlackholeProxy implements AutoCloseable {

  private final ServerSocket server;
  private final String targetHost;
  private final int targetPort;
  private final List<Link> links = new CopyOnWriteArrayList<>();

  public BlackholeProxy(String targetHost, int targetPort) throws IOException {
    this.targetHost = targetHost;
    this.targetPort = targetPort;
    this.server = new ServerSocket();
    this.server.bind(new InetSocketAddress("127.0.0.1", 0));
    Thread acceptor = new Thread(this::acceptLoop, "blackhole-proxy-accept");
    acceptor.setDaemon(true);
    acceptor.start();
  }

  public int port() {
    return server.getLocalPort();
  }

  // 지금까지 맺어진 연결은 소켓을 연 채로 데이터만 버림. 새 연결은 그대로 중계
  public void blackholeExisting() {
    links.forEach(link -> link.dead.set(true));
  }

  private void acceptLoop() {
    while (!server.isClosed()) {
      try {
        Socket client = server.accept();
        Socket upstream = new Socket(targetHost, targetPort);
        Link link = new Link();
        links.add(link);
        pipe(client, upstream, link);
        pipe(upstream, client, link);
      } catch (IOException e) {
        return;
      }
    }
  }

  private static void pipe(Socket from, Socket to, Link link) {
    Thread thread = new Thread(() -> {
      byte[] buffer = new byte[8192];
      try (InputStream in = from.getInputStream(); OutputStream out = to.getOutputStream()) {
        int read;
        while ((read = in.read(buffer)) != -1) {
          if (!link.dead.get()) {
            out.write(buffer, 0, read);
            out.flush();
          }
        }
      } catch (IOException ignored) {
        // 연결 종료
      }
    }, "blackhole-proxy-pipe");
    thread.setDaemon(true);
    thread.start();
  }

  @Override
  public void close() throws IOException {
    server.close();
  }

  private static final class Link {

    private final AtomicBoolean dead = new AtomicBoolean(false);
  }
}
