@echo off
if exist c:\Python34\python.exe (
	C:\Python34\python.exe awsLogFileUpload.py
) else (
	if exist c:\Python37\python.exe (
		C:\Python37\python.exe awsLogFileUpload.py
	) else (
		if exist C:\Users\FIRSTUser\AppData\Local\Programs\Python\Python37-32\python.exe (
			C:\Users\FIRSTUser\AppData\Local\Programs\Python\Python37-32\python.exe awsLogFileUpload.py
		) else (
			python awsLogFileUpload.py
		)
	)
)

pause 
