<%-- 
    Document   : 404
    Created on : Jul 6, 2025, 6:54:12 PM
    Author     : ADMIN
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.0.0-alpha.6/css/bootstrap.min.css">
        <script  src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
        <title>404 Not Found</title>
        <style>
            @import url("https://fonts.googleapis.com/css?family=Comfortaa:300,400,700");
            .not-found, .four0four {
                font-family: "Comfortaa";
            }

            * {
                box-sizing: border-box;
            }

            html {
                height: 100vh;
            }

            body {
                background: #121428;
                margin: 0;
                padding: 0;
                overflow: hidden;
                height: 100vh;
            }

            .container {
                align-items: center;
                margin: auto;
            }

            .row {
                margin-top: 30vh;
                height: 200px;
                min-width: 300px;
            }

            .four0four {
                color: white;
                text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                color: white;
                margin-top: -67px;
                font-size: 15em;
                text-align: right;
                min-width: 450px;
                z-index: 5;
            }
            .four0four:before {
                content: "404";
                position: absolute;
                color: #ff78fa;
                mix-blend-mode: color-dodge;
                z-index: 1;
            }

            .not-found {
                color: white;
                text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                line-height: 110%;
                font-size: 3.75em;
            }
            .not-found:before {
                letter-spacing: 0.25px;
                content: "Page not found";
                position: absolute;
                color: #ff78fa;
                mix-blend-mode: color-dodge;
                z-index: 1;
            }

            .flicker1 {
                animation: 5s linear 3.25s flickr infinite;
            }

            .flicker2 {
                animation: 5s linear 1.25s flickr infinite;
            }

            .flicker3 {
                animation: 5s ease 1s flickr infinite;
            }

            .flicker4 {
                animation: 5s ease 2s flickr infinite;
            }

            .off {
                color: rgba(50, 50, 50, 0.25);
                border: none;
                text-shadow: none;
            }

            @keyframes flickr {
                0% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                1% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
                2% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                8% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                10% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
                11% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                12% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
                13% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                14% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
                53% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
                54% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                58% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                59% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
                60% {
                    color: rgba(50, 50, 50, 0.25);
                    border: none;
                    text-shadow: none;
                }
                61% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
                100% {
                    color: white;
                    text-shadow: 0 0 1px rgba(255, 120, 250, 0.2), 0 0 2px rgba(200, 150, 250, 0.9), 0 0 70px #c896fa;
                }
            }
            .fog8, .fog7, .fog6, .fog5, .fog4, .fog3, .fog2, .fog1 {
                position: absolute;
                border: none;
                border-radius: 100%;
                z-index: 400;
            }

            .fog1 {
                width: 100px;
                height: 100px;
                animation: 75s linear float infinite;
                box-shadow: inset 0 0 50px rgba(255, 255, 255, 0.8), 0 0 50px rgba(255, 255, 255, 0.5);
            }

            .fog2 {
                width: 50px;
                height: 50px;
                box-shadow: inset 0 0 25px rgba(255, 255, 255, 0.8), 0 0 25px rgba(255, 255, 255, 0.5);
                margin-top: -25%;
                animation: 25s linear float2 infinite;
            }

            .fog3 {
                width: 80px;
                height: 80px;
                box-shadow: inset 0 0 40px rgba(255, 255, 255, 0.8), 0 0 40px rgba(255, 255, 255, 0.5);
                margin-left: 25%;
                animation: 40s ease float2 infinite;
            }

            .fog4 {
                width: 40px;
                height: 40px;
                margin-left: 60%;
                margin-top: -45%;
                animation: 45s linear float infinite;
                box-shadow: inset 0 0 20px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);
            }

            .fog5 {
                width: 60px;
                height: 60px;
                margin-left: 50%;
                margin-top: -30%;
                animation: 30s ease float2 infinite;
                box-shadow: inset 0 0 30px rgba(255, 255, 255, 0.8), 0 0 30px rgba(255, 255, 255, 0.5);
            }

            .fog5 {
                width: 125px;
                height: 125px;
                margin-left: 50%;
                margin-top: 30%;
                animation: 70s ease-out float3 infinite;
                box-shadow: inset 0 0 40px rgba(255, 255, 255, 0.8), 0 0 60px rgba(255, 255, 255, 0.5);
            }

            .fog6 {
                width: 20px;
                height: 20px;
                margin-left: 50%;
                margin-top: -10%;
                animation: 20s linear float infinite;
                box-shadow: inset 0 0 2px rgba(255, 255, 255, 0.8), 0 0 2px rgba(255, 255, 255, 0.5);
            }

            .fog7 {
                width: 50px;
                height: 50px;
                margin-left: 0%;
                margin-top: 30%;
                animation: 40s ease-out float3 infinite;
                box-shadow: inset 0 0 20px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);
            }

            .fog8 {
                width: 80px;
                height: 80px;
                margin-left: 60%;
                margin-top: -20%;
                animation: 40s ease-out float3 infinite;
                box-shadow: inset 0 0 20px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);
            }

            .btn-home {
                display: inline-block;
                padding: 12px 25px;
                font-size: 1.2em;
                font-weight: bold;
                color: #ff78fa;
                border: 2px solid #ff78fa;
                border-radius: 30px;
                background-color: transparent;
                text-decoration: none;
                transition: 0.3s ease;
                box-shadow: 0 0 10px rgba(255, 120, 250, 0.4);
            }

            .btn-home:hover {
                background-color: #ff78fa;
                color: #121428;
                box-shadow: 0 0 20px #ff78fa, 0 0 40px #ff78fa;
            }


            @keyframes float {
                0% {
                    top: 50%;
                    left: 10%;
                }
                25% {
                    top: 75%;
                    left: 25%;
                }
                50% {
                    top: 50%;
                    left: 35%;
                }
                75% {
                    top: 75%;
                    left: 25%;
                }
                100% {
                    top: 50%;
                    left: 10%;
                }
            }
            @keyframes float2 {
                0% {
                    top: 50%;
                    left: 10%;
                }
                25% {
                    top: 25%;
                    left: 15%;
                }
                50% {
                    top: 50%;
                    left: 45%;
                }
                75% {
                    top: 25%;
                    left: 15%;
                }
                100% {
                    top: 50%;
                    left: 10%;
                }
            }
            @keyframes float3 {
                0% {
                    top: 50%;
                    left: 25%;
                }
                25% {
                    top: 35%;
                    left: 0;
                }
                50% {
                    top: 10%;
                    left: 10%;
                }
                75% {
                    top: 35%;
                    left: 0%;
                }
                100% {
                    top: 50%;
                    left: 25%;
                }
            }
            @media (max-width: 991px) {
                .container {
                    margin-top: 30vh;
                }

                .four0four {
                    text-align: center;
                    min-width: 100%;
                    padding: 0;
                    margin: 0;
                    height: 200px;
                    font-size: 10em;
                }

                .not-found {
                    font-size: 1.75em;
                    padding: 0;
                    margin: 0;
                    text-align: center;
                }
            }


        </style>
    </head>
    <body>
        <div class="container col-md-8">
            <div class="row">
                <div class="four0four col-md-4 col-sm-12 offset-md-2"><span class="off">4</span><span class="flicker2">0</span><span class="flicker3">4</span></div>
                <div class="not-found col-lg-2 col-md-12"><span class="flicker4">P</span><span class="off">a</span><span>ge </span><span>not </span><span class="off">f</span><span class="flicker1">o</span><span class="flicker3">u</span><span>n</span><span class="off">d</span></div>
                <!--          <div class="col-12 text-center mt-5">
                            <a href="${request.getContextPath()}home" class="btn-home">← Back to Home</a>
                          </div>-->
                <div class="fog0"></div>
                <div class="fog1"></div>
                <div class="fog2"></div>
                <div class="fog3"></div>
                <div class="fog4"></div>
                <div class="fog5"></div>
                <div class="fog6"></div>
                <div class="fog7"></div>
                <div class="fog8"></div>
                <div class="fog9"></div>
            </div>
        </div>
    </body>

</html>


