*** Settings ***
Library  Selenium2Library
Documentation
...    Login Test Case.


***Variables***
${Browser}  Chrome
${URL}  http://www.google.com


*** Test Cases ***
TC_001 Browser Start and Close

	Open Browser  ${URL}  ${Browser}
	Input Text  id=username  demo
	Input Password  id=password  abc123
	Click Button  xpath=//*[@id="login1"]/form/div[9]/button
	Close Browser