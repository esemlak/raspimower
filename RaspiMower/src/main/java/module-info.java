open module hns.erse {
    // Module Exports
    exports hns.erse;

    // Pi4J Modules
    requires com.pi4j;
    requires com.pi4j.library.pigpio;
    requires com.pi4j.plugin.pigpio;
    requires com.pi4j.plugin.raspberrypi;
    uses com.pi4j.extension.Extension;
    uses com.pi4j.provider.Provider;



    // SLF4J Modules
    //requires org.slf4j;
    //requires org.slf4j.simple;

    // PicoCLI Modules
    requires info.picocli;
    requires java.desktop;
    requires com.fazecast.jSerialComm;
    requires jline;
    requires ejml.core;
    requires ejml.ddense;
    requires ejml.simple;


    // AWT
}
