param()

Write-Host "Installing shared module..."
mvn -q -pl common-events -am install -DskipTests

$modules = @(
  "product-service",
  "order-service",
  "payment-service",
  "notification-service",
  "dashboard-ui"
)

foreach ($m in $modules) {
  Write-Host "Starting $m"
  Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd C:\00_INTERVIEW\TinyCorpLaptopTracker; mvn -pl $m spring-boot:run"
}
