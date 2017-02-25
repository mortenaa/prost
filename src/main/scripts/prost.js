WshShell = WScript.CreateObject("WScript.Shell");
PROST_JAR="prost.jar"
JAVA_CMD="java -jar"
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
            WScript.Echo("Error: To few arguments!");
        } else {
            fMin=WScript.Arguments(i+1)*1000
            fMax=WScript.Arguments(i+2)*1000
            fStep=WScript.Arguments(i+3)*1000
            i=i+3
        }
    } else
        args=args+" "+WScript.Arguments(i)
}
//WScript.Echo(args)
//WScript.Echo(fMin)
if(!doFRange) {
    cmd=JAVA_CMD+" "+PROST_JAR+" "+args;
    WshShell.Run(cmd,7,true)
} else {
    for (f=fMin; f<=fMax; f+=fStep) {
        CScript.Echo("F=" + f)
        cmd=JAVA_CMD+" "+PROST_JAR+" -f "+f/1000+args;
        WshShell.Run(cmd,5,true)
    }
}