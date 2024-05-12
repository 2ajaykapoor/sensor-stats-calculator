# Sensors Data Calculator

This repository is designed for calculating sensor data. After running the command `sbt "run path/of/your/csv/data/directory"`, it will process the specified CSV data files and provide a summary as follows:

1. **Processed Files:** The total number of files processed.
2. **Processed Measurements:** The total count of measurements across all files.
3. **Succeeded Measurements:** The count of successful measurements.
4. **Failed Measurements:** The count of failed measurements.

Additionally, it will display sensor-specific statistics:

```plaintext
Sensor $sensorId || Min=$min, Avg=$avg, Max=$max
```
## Prerequisites

Make sure you have Java, Scala and SBT installed on your system before running the project.

## Usage
1. Clone this repository.
2. Open a terminal and navigate to the project directory.
3. Run the following command
   
```plaintext
sbt "run path/of/your/csv/data/directory"
```

