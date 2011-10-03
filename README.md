BAckEnd System SImulator

Requirements:
SBT is needed to build, get it here https://github.com/harrah/xsbt

Once you have SBT installed it's just a matter of running SBT, type `compile` followed by `project simulator` and then `jetty-run` 
which will start jetty at port 8080 with the simulator at the root-context.

Load test-data into the simulator by calling the simulator with

    http://localhost:8080/ws/setup?testId=test1&request=<hello></hello>&response=<world></world>

Any call to the simulator on a non-page URL will try to match the posted data with the request of the loaded test data and return 
the response of the first matching request.

