# Load environment variables from .env file
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^=]+)=(.*)$') {
        $name = $matches[1]
        $value = $matches[2]
        Set-Item -Path "env:$name" -Value $value
        Write-Host "Loaded: $name"
    }
}

Write-Host "`nStarting Spring Boot application..."
.\mvnw.cmd spring-boot:run
