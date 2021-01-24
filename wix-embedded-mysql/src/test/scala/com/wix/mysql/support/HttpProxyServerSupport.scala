package com.wix.mysql.support

import java.io.{File, FileInputStream}
import java.nio.file.Files
import java.util.UUID
import com.wix.mysql.PackagePaths
import com.wix.mysql.distribution.WixVersion
import de.flapdoodle.embed.process.distribution.Distribution
import de.flapdoodle.embed.process.io.directories.UserHome
import fi.iki.elonen.NanoHTTPD
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import org.littleshoot.proxy.{ActivityTrackerAdapter, FlowContext, HttpProxyServer}

trait HttpProxyServerSupport {

  def withProxyOn[T](proxyPort: Int, targetPort: Int, servedVersion: WixVersion)(f: (ConnectedActivityTracker, Int, Int, WixVersion) => T): T = {
    val tracker = new ConnectedActivityTracker()
    val proxyBootstrap = DefaultHttpProxyServer
      .bootstrap()
      .plusActivityTracker(tracker)
      .withPort(proxyPort)

    var proxyServer: Option[HttpProxyServer] = None
    var staticsServer: Option[StaticsServer] = None
    val (fileToProxy, restoreFiles) = ProxyFiles(servedVersion)
    try {
      staticsServer = new StaticsServer(targetPort, fileToProxy).doStart()
      proxyServer = Some(proxyBootstrap.start())

      f(tracker, proxyPort, targetPort, servedVersion)
    } finally {
      proxyServer.foreach(_.stop())
      staticsServer.foreach(_.stop())
      restoreFiles()
    }
  }
}

class ConnectedActivityTracker extends ActivityTrackerAdapter {
  val fiftyMegabytesInBytes = 50000000
  var bytesSent: Int = 0

  override def bytesSentToClient(flowContext: FlowContext, numberOfBytes: Int): Unit = {
    bytesSent += numberOfBytes
  }

  def wasDownloaded: Boolean = bytesSent > fiftyMegabytesInBytes
}

class StaticsServer(port: Int, fileToServe: File) extends NanoHTTPD(port) {

  def doStart(): Option[StaticsServer] = {
    start()
    Some(this)
  }

  override def serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response = {
    NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.ACCEPTED, "application/tar+gzip", new FileInputStream(fileToServe))
  }
}

object ProxyFiles {
  val tmpDir: File = new UserHome(".embedmysql").asFile()
  type Restore = (File, () => Unit)

  def apply(version: WixVersion): Restore = {
    val source = new File(tmpDir, new PackagePaths().getPath(Distribution.detectFor(version)))
    val target = new File(tmpDir, UUID.randomUUID().toString)
    if (!target.exists()) {
      Files.copy(source.toPath, target.toPath)
    }
    Files.delete(source.toPath)

    (target, () => {
      if (!source.exists()) {
        Files.copy(target.toPath, source.toPath)
      }
      Files.delete(target.toPath)
    })

  }
}
