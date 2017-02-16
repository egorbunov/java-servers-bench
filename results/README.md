# Results

## Variable array length (message length)

### Benchmark parameters:

* Client Number = `4`
* Receive-Send Delay = `0`
* Per-client Request Number = `10`
* Array length, variable from `100` to `5000` with step = `100`
* Number of repeats = `5`

### Charts

![Receive-send gap chart](1_arr_len/receive_send_time.png)
![Sort time chart](1_arr_len/sort_time.png)
![Client lifespan](1_arr_len/client_lifespan.png)

## Variable client number

### Benchmark parameters:

* Receive-Send Delay = `5`
* Per-client Request Number = `100`
* Array length = `500`
* Client Number, variable from = `1` to `30` with step = `2`
* Number of repeats = `1`

### Charts

![Receive-send gap chart](2_client_num/receive_send_time.png)
![Sort time chart](2_client_num/sort_time.png)
![Client lifespan](2_client_num/client_lifespan.png)

## Variable request number

### Benchmark parameters:

* Receive-Send Delay = `5`
* Client Number = `4`
* Array length = `500`
* Per-client Request Number, variable from `10` to `150` with step = `10`
* Number of repeats = `5`

### Charts

![Receive-send gap chart](3_request_num/receive_send_time.png)
![Sort time chart](3_request_num/sort_time.png)
![Client lifespan](3_request_num/client_lifespan.png)


## Variable receive-send delay

### Benchmark parameters:

* Client Number = `4`
* Array length = `1000`
* Per-client Request Number = `100`
* Receive-Send Delay, variable from = `0ms`ms to `100ms` with step = `10`
* Number of repeats = `1`

### Charts

![Receive-send gap chart](4_send_rcv_gap/receive_send_time.png)
![Sort time chart](4_send_rcv_gap/sort_time.png)
![Client lifespan](4_send_rcv_gap/client_lifespan.png)