Scala Utils

A collection of utilities intended for data-mining/logging apps

## Logger usage
To send messages to the logger

```
import utils.Concise.now
import utils.Singleton.logger

logger !
        ( "C:\logPath.txt",
          Timestamp(
            Namespace("YouTube", "New Video"),
            now)
          )
```
Output: C:\logPath.txt
```
Youtube New Video               2016-02-11	16:08:06
```
