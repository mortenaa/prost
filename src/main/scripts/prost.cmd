@if (@CodeSection == @Batch) @then
    @cscript //Nologo //E:jscript "%~f0" %* & goto :eof
@end

WshShell = WScript.CreateObject("WScript.Shell");
PROST_JAR="prost.jar"
env = WshShell.Environment("SYSTEM")
jhome = env("JAVA_HOME")
JAVA_CMD=jhome + "\\bin\\" + "java -jar"

File=new ActiveXObject("Scripting.FileSystemObject")

if(!File.FileExists(PROST_JAR)) {
    PROST_JAR="../"+PROST_JAR
    if(!File.FileExists(PROST_JAR))
        WScript.Echo("Error: Could not find prost.jar")
}
doFRange=false;
args=""
for(i=0; i<WScript.Arguments.length; i++) {
    if(WScript.Arguments(i).toLowerCase()=="-f") {
        doFRange=true;
        if(i+3>=WScript.Arguments.length) {
            WScript.Echo("Error: To few arguments!")
        } else {
            fMin=WScript.Arguments(i+1)*1000
            fMax=WScript.Arguments(i+2)*1000
            fStep=WScript.Arguments(i+3)*1000
            i=i+3
        }
    } else
        args=args+" "+WScript.Arguments(i)
}

if(!doFRange) {
    cmd=JAVA_CMD + " " + PROST_JAR + " " + args;
	WScript.Echo("Running: '" + cmd + "'")
    WshShell.Run(cmd, 0, true)
} else {
    for (f=fMin; f<=fMax; f+=fStep) {
        cmd=JAVA_CMD + " " + PROST_JAR + " -f " + f/1000 + args;
        WScript.Echo("Running: '" + cmd + "'")
        err = WshShell.Run(cmd, 0, true)
		if (err != 0) {
			WScript.Echo("Error code: " + err)
		}
    }
}