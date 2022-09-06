## Commands

Scripts in this folder can be executed in order to perform functionalities, in particular:

  - *init_xx.sh*: sends all selected files for the testing session to the microservice environment.
  - *retrieveSpec_xx.sh*: automatically retrieve microservice's specification, trough information defined in a json file (e.g., [ports_ftgo.json](https://github.com/uDEVOPS2020/MacroHive/blob/main/uTest/clientCommands/initFiles/ports_ftgo.json)). Represent an alternative to the previous instruction, where specification files are needed.
  - *execute.sh*: start the testing session.
  - *clear.sh*: clear the microservice's environment (e.g., to perform a new testing session).
  - *getOutput.sh*: retrieve all output information after a testing session, and put the files in [output](https://github.com/uDEVOPS2020/MacroHive/blob/main/uTest/clientCommands/initFiles) folder. Must be called before clear.

!! initialization files (in [initFiles](https://github.com/uDEVOPS2020/MacroHive/blob/main/uTest/clientCommands/initFiles)) must be configured before script calls.
