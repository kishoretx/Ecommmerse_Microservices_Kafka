param(
    [int]$TimeoutSeconds = 180,
    [int]$IntervalSeconds = 5
)

$ErrorActionPreference = 'Stop'

$targets = @(
    @{ Name = 'kafka'; Url = 'http://localhost:9092'; Kind = 'tcp' },
    @{ Name = 'kafka-ui'; Url = 'http://localhost:8090/actuator/health'; Kind = 'http' },
    @{ Name = 'product-service'; Url = 'http://localhost:8081/actuator/health'; Kind = 'http' },
    @{ Name = 'order-service'; Url = 'http://localhost:8082/actuator/health'; Kind = 'http' },
    @{ Name = 'payment-service'; Url = 'http://localhost:8083/actuator/health'; Kind = 'http' },
    @{ Name = 'notification-service'; Url = 'http://localhost:8084/actuator/health'; Kind = 'http' },
    @{ Name = 'dashboard-ui'; Url = 'http://localhost:8080/actuator/health'; Kind = 'http' }
)

function Test-HttpHealthy {
    param([string]$Url)

    try {
        $response = Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 5
        if ($null -ne $response.status -and $response.status -eq 'UP') {
            return $true
        }
        return $true
    }
    catch {
        return $false
    }
}

function Test-TcpPort {
    param([string]$Url)

    try {
        $uri = [System.Uri]$Url
        $client = New-Object System.Net.Sockets.TcpClient
        $iar = $client.BeginConnect($uri.Host, $uri.Port, $null, $null)
        $success = $iar.AsyncWaitHandle.WaitOne(3000, $false)
        if (-not $success) {
            $client.Close()
            return $false
        }
        $client.EndConnect($iar)
        $client.Close()
        return $true
    }
    catch {
        return $false
    }
}

$start = Get-Date
$deadline = $start.AddSeconds($TimeoutSeconds)

Write-Host "Smoke test started at $(Get-Date -Format o)"
Write-Host "Timeout: $TimeoutSeconds seconds, interval: $IntervalSeconds seconds"
Write-Host ""

$final = @{}

while ((Get-Date) -lt $deadline) {
    $allHealthy = $true

    foreach ($target in $targets) {
        $ok = if ($target.Kind -eq 'http') {
            Test-HttpHealthy -Url $target.Url
        } else {
            Test-TcpPort -Url $target.Url
        }

        $final[$target.Name] = $ok
        if (-not $ok) { $allHealthy = $false }
    }

    if ($allHealthy) { break }
    Start-Sleep -Seconds $IntervalSeconds
}

Write-Host "========== Smoke Test Report =========="
$passed = $true
foreach ($target in $targets) {
    $ok = $final[$target.Name]
    $status = if ($ok) { 'PASS' } else { 'FAIL' }
    if (-not $ok) { $passed = $false }
    Write-Host ("{0,-22} {1,-5} {2}" -f $target.Name, $status, $target.Url)
}

Write-Host "======================================="

if ($passed) {
    Write-Host "Overall result: PASS"
    exit 0
}

Write-Host "Overall result: FAIL"
exit 1
