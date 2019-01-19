@echo off
if exist c:\Python34\python.exe (
	C:\Python34\python.exe file_snagger\snag_files.py
) else (
	if exist c:\Python37\python.exe (
		C:\Python37\python.exe file_snagger\snag_files.py
	) else (
		python file_snagger\snag_files.py
	)
)

pause 
