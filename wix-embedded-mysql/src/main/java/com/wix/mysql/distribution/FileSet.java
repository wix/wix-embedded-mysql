package com.wix.mysql.distribution;

import com.wix.mysql.distribution.fileset.*;
import de.flapdoodle.embed.process.collections.Collections;
import de.flapdoodle.embed.process.distribution.Platform;

import java.util.List;

public class FileSet {
    private static List<FileSetEmitter> emitters = Collections.newArrayList(
            new Win56FileSetEmitter(),
            new Win57FileSetEmitter(),
            new Win57_18_UpFileSetEmitter(),
            new Win8FileSetEmitter(),
            new Nix55FileSetEmitter(),
            new Nix56FileSetEmitter(),
            new Nix57FileSetEmitter(),
            new Nix57_18_AndUpFileSetEmitter(),
            new Nix8FileSetEmitter(),
            new OSX8FileSetEmitter());

    public static de.flapdoodle.embed.process.config.store.FileSet emit(
            final Platform platform,
            final WixVersion version) {

        for (FileSetEmitter emitter : emitters) {
            if (emitter.matches(platform, version)) {
                return emitter.emit();
            }
        }
        throw new RuntimeException(String.format("FileSetEmitter not found for platform: %s version: %s", platform, version.toString()));
    }

}
