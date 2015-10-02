<!doctype html>

<html lang="en">

<head>

  <meta charset="utf-8">

  <title>jQuery UI Tabs - Default functionality</title>

  <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">

  <script src="//code.jquery.com/jquery-1.10.2.js"></script>

  <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>

  <link rel="stylesheet" href="/resources/demos/style.css">

  <script>

  $(function() {

    $( "#tabs" ).tabs();

  });

  </script>

</head>

<body>

 

<div id="tabs">

  <ul>

    <li><a href="#tabs-1">My Items List</a></li>

    <li><a href="#tabs-2">Marketplace</a></li>

  </ul>

  <div id="tabs-1">

    <p>Students items.</p>

  </div>

  <div id="tabs-2">

    <p>All available items for purhcase.</p>

  </div>

</div>

 

 

</body>

</html>

