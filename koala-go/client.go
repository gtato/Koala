package koala

import (
    "fmt"
    "net/http"
    "io/ioutil"
    "bytes"
    )


type Client struct {
    Url      string
}

func (c *Client) Version() error {
    response, err := http.Get(c.Url + "/version")
    if err != nil {
        fmt.Printf("%s", err)
    } else {
        defer response.Body.Close()
        contents, err := ioutil.ReadAll(response.Body)
        if err != nil {
            fmt.Printf("%s", err)
            return err
        }
        fmt.Printf("%s\n", string(contents))
    }
    return err
}

func (c *Client) ApiPost(method string, params string) error{
    url := c.Url+"/api/" +method

    var jsonStr = []byte(params)
    req, err := http.NewRequest("POST", url, bytes.NewBuffer(jsonStr))
    req.Header.Set("Content-Type", "application/json")

    client := &http.Client{}
    resp, err := client.Do(req)
    if err != nil {
        fmt.Printf("%s", err)
        return err
    }
    defer resp.Body.Close()

    body, _ := ioutil.ReadAll(resp.Body)
    fmt.Println("response Body:", string(body))
    return err
}

