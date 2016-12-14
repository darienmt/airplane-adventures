# Airplane Adventures

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/darienmt/airplane-adventures/master/LICENSE)
[![Build status](https://img.shields.io/travis/darienmt/airplane-adventures/master.svg)](https://travis-ci.org/darienmt/airplane-adventures)

This project explores Dump1090/Flightaware feeder messengers.
It consists on three modules:

- modules/basestation-repeater: Repeats the basestation communication into a local port. It is great to test what happens if the feeder stop working.
- modules/basestation-data: Case classes to represent the basestation messages.
- modules/basestation-collector: Collect the basestation messages from the feeder and stores them on a kafka topic.
- modules/basestation-raw-collector: Collect the basestation messages from the feeder and stores them on a kafka topic as they come from the feeder.

