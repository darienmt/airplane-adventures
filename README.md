# Airplane Adventures

This project explores Dump1090/Flightaware feeder messengers.
It consists on three modules:

- modules/basestation-repeater: Repeats the basestation communication into a local port. It is great to test what happens if the feeder stop working.
- modules/basestation-data: Case classes to represent the basestation messages.
- modules/basestation-collector: Collect the basestation messages from the feeder and stores them on a kafka topic.

