Scala Logger

This logging library is intended for quick prototyping of data-mining/logging apps where the schema has yet to be finalized.

## Logger usage
To send messages to the logger

```
import utils.Concise.now
import utils.Singleton.logger

logger !
        SingleTimestamp(
          "C:\logPath.txt",
          Timestamp(
            Namespace("YouTube", "New Video"),
            now)
          )
```
Output: C:\logPath.txt
```
Youtube New Video               2016-02-11	16:08:06
```
