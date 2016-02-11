Scala Utils

A collection of utilities intended for data-mining/logging apps

## Logger usage
To send messages to the logger

```
import utils.Concise._
import utils.Singleton.logger

logger !
        ( logPath,
          Timestamp(
            Namespace("YouTube", "New Video"),
            now)
          )
```