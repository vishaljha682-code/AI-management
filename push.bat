@echo off
set "PATH=C:\Users\Asus\MinGit\cmd;C:\Users\Asus\MinGit\mingw64\bin;%PATH%"
cd /d "%~dp0"
echo ========================================================
echo  AI ResearchHub - Pushing to GitHub (origin/main)
echo  Remote: https://github.com/vishaljha682-code/AI-management.git
echo ========================================================
git push -u origin main
echo.
pause
