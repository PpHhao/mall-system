$ErrorActionPreference = "Stop"

$baseUrl = "http://localhost:8080/api"
if ($env:BASE_URL) { $baseUrl = $env:BASE_URL }

$username = "admin"
if ($env:API_USER) { $username = $env:API_USER }

$password = "Admin@123"
if ($env:API_PASS) { $password = $env:API_PASS }

$productId = 1
if ($env:PRODUCT_ID) { $productId = [int]$env:PRODUCT_ID }

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        $Body = $null,
        [hashtable]$Headers = $null
    )
    $uri = "$baseUrl$Path"
    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 10
        return Invoke-RestMethod -Method $Method -Uri $uri -Headers $Headers -ContentType "application/json" -Body $jsonBody
    }
    return Invoke-RestMethod -Method $Method -Uri $uri -Headers $Headers
}

function Assert-Ok {
    param(
        $Response,
        [string]$Label
    )
    if ($null -eq $Response) {
        throw "[$Label] No response"
    }
    if ($Response.code -ne 0) {
        throw "[$Label] code=$($Response.code) message=$($Response.message)"
    }
}

Write-Host "Login..."
$loginRes = Invoke-Api -Method "POST" -Path "/auth/login" -Body @{
    username = $username
    password = $password
}
Assert-Ok $loginRes "Login"
$tokenType = $loginRes.data.tokenType
if (-not $tokenType) { $tokenType = "Bearer" }
$authHeaderValue = "$tokenType $($loginRes.data.accessToken)"
$authHeaders = @{ Authorization = $authHeaderValue }

Write-Host "Create address..."
$addressRes = Invoke-Api -Method "POST" -Path "/addresses" -Body @{
    receiverName = "Test User"
    receiverPhone = "13800000000"
    province = "Guangdong"
    city = "Shenzhen"
    district = "Nanshan"
    detail = "Tech Park 1st Street"
    postalCode = "518000"
    isDefault = 1
} -Headers $authHeaders
Assert-Ok $addressRes "Create Address"
$addressId = $addressRes.data.id
if (-not $addressId) { throw "[Create Address] addressId missing" }

Write-Host "List addresses..."
$addressesRes = Invoke-Api -Method "GET" -Path "/addresses" -Headers $authHeaders
Assert-Ok $addressesRes "List Addresses"

Write-Host "Add cart item..."
$cartAddRes = Invoke-Api -Method "POST" -Path "/cart/items" -Body @{
    productId = $productId
    quantity = 1
} -Headers $authHeaders
Assert-Ok $cartAddRes "Add Cart Item"
$cartItemId = $cartAddRes.data.id
if (-not $cartItemId) { throw "[Add Cart Item] cartItemId missing" }

Write-Host "Update cart item quantity..."
$cartUpdateRes = Invoke-Api -Method "PUT" -Path "/cart/items/$cartItemId" -Body @{
    quantity = 2
} -Headers $authHeaders
Assert-Ok $cartUpdateRes "Update Cart Item"

Write-Host "List cart items..."
$cartListRes = Invoke-Api -Method "GET" -Path "/cart" -Headers $authHeaders
Assert-Ok $cartListRes "List Cart Items"

Write-Host "Create order..."
$orderCreateRes = Invoke-Api -Method "POST" -Path "/orders" -Body @{
    addressId = $addressId
    remark = "Smoke test order"
    payMethod = 1
    items = @(
        @{
            productId = $productId
            quantity = 1
        }
    )
} -Headers $authHeaders
Assert-Ok $orderCreateRes "Create Order"
$orderId = $orderCreateRes.data.id
if (-not $orderId) { throw "[Create Order] orderId missing" }

Write-Host "Order detail..."
$orderDetailRes = Invoke-Api -Method "GET" -Path "/orders/$orderId" -Headers $authHeaders
Assert-Ok $orderDetailRes "Order Detail"
if ($orderDetailRes.data.status -ne 1) { throw "[Order Detail] expected status 1" }

Write-Host "Pay order..."
$payRes = Invoke-Api -Method "PUT" -Path "/orders/$orderId/pay" -Body @{
    payMethod = 1
} -Headers $authHeaders
Assert-Ok $payRes "Pay Order"

$orderDetailRes = Invoke-Api -Method "GET" -Path "/orders/$orderId" -Headers $authHeaders
Assert-Ok $orderDetailRes "Order Detail After Pay"
if ($orderDetailRes.data.status -ne 2) { throw "[Order Detail After Pay] expected status 2" }

Write-Host "Ship order (admin)..."
$shipRes = Invoke-Api -Method "PUT" -Path "/admin/orders/$orderId/ship" -Headers $authHeaders
Assert-Ok $shipRes "Ship Order"

$orderDetailRes = Invoke-Api -Method "GET" -Path "/orders/$orderId" -Headers $authHeaders
Assert-Ok $orderDetailRes "Order Detail After Ship"
if ($orderDetailRes.data.status -ne 3) { throw "[Order Detail After Ship] expected status 3" }

Write-Host "Complete order..."
$completeRes = Invoke-Api -Method "PUT" -Path "/orders/$orderId/complete" -Headers $authHeaders
Assert-Ok $completeRes "Complete Order"

$orderDetailRes = Invoke-Api -Method "GET" -Path "/orders/$orderId" -Headers $authHeaders
Assert-Ok $orderDetailRes "Order Detail After Complete"
if ($orderDetailRes.data.status -ne 4) { throw "[Order Detail After Complete] expected status 4" }

Write-Host "List orders..."
$ordersRes = Invoke-Api -Method "GET" -Path "/orders?status=4&page=1&pageSize=10" -Headers $authHeaders
Assert-Ok $ordersRes "List Orders"

Write-Host "Order stats (admin)..."
$statsRes = Invoke-Api -Method "GET" -Path "/admin/orders/stats" -Headers $authHeaders
Assert-Ok $statsRes "Order Stats"

Write-Host "Smoke test completed successfully."
