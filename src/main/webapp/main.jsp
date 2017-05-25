<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<!-- <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

</body>
</html> -->

<!doctype html>
<html>
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link href="js/mui-0.9.16/css/mui.min.css" rel="stylesheet" type="text/css" />
<script src="js/mui-0.9.16/js/mui.min.js"></script>
<script src="js/jquery-3.2.1.min.js"></script>
<style>
/**
 * Body CSS
 */
html, body {
  height: 100%;
  background-color: #eee;
}

html, body, input, textarea, buttons {
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.004);
}

/**
 * Layout CSS
 */
#header {
  position: fixed;
  top: 0;
  right: 0;
  left: 0;
  z-index: 2;
  transition: left 0.2s;
}

#sidedrawer {
  position: fixed;
  top: 0;
  bottom: 0;
  width: 200px;
  left: -200px;
  overflow: auto;
  z-index: 2;
  background-color: #fff;
  transition: transform 0.2s;
}

#content-wrapper {
  min-height: 100%;
  overflow-x: hidden;
  margin-left: 0px;
  transition: margin-left 0.2s;
  /* sticky bottom */
  margin-bottom: -160px;
  padding-bottom: 160px;
}

#footer {
  height: 160px;
  margin-left: 0px;
  transition: margin-left 0.2s;
}

@media ( min-width : 768px) {
  #header {
    left: 200px;
  }
  #sidedrawer {
    transform: translate(200px);
  }
  #content-wrapper {
    margin-left: 200px;
  }
  #footer {
    margin-left: 200px;
  }
  body.hide-sidedrawer #header {
    left: 0;
  }
  body.hide-sidedrawer #sidedrawer {
    transform: translate(0px);
  }
  body.hide-sidedrawer #content-wrapper {
    margin-left: 0;
  }
  body.hide-sidedrawer #footer {
    margin-left: 0;
  }
}

/**
 * Toggle Side drawer
 */
#sidedrawer.active {
  transform: translate(200px);
}

/**
 * Header CSS
 */
.sidedrawer-toggle {
  color: #fff;
  cursor: pointer;
  font-size: 20px;
  line-height: 20px;
  margin-right: 10px;
}

.sidedrawer-toggle:hover {
  color: #fff;
  text-decoration: none;
}

/**
 * Footer CSS
 */
#footer {
  background-color: #0288D1;
  color: #fff;
}

#footer a {
  color: #fff;
  text-decoration: underline;
}
</style>
<script>
  jQuery(function($) {
    var $bodyEl = $('body'), $sidedrawerEl = $('#sidedrawer');

    function showSidedrawer() {
      // show overlay
      var options = {
        onclose: function() {
          $sidedrawerEl.removeClass('active').appendTo(document.body);
        }
      };

      var $overlayEl = $(mui.overlay('on', options));

      // show element
      $sidedrawerEl.appendTo($overlayEl);
      setTimeout(function() {
        $sidedrawerEl.addClass('active');
      }, 20);
    }

    function hideSidedrawer() {
      $bodyEl.toggleClass('hide-sidedrawer');
    }

    $('.js-show-sidedrawer').on('click', showSidedrawer);
    $('.js-hide-sidedrawer').on('click', hideSidedrawer);
  });
</script>
</head>
<body>
  <div id="sidedrawer" class="mui--no-user-select">
    <!-- Side drawer content goes here -->
  </div>
  <header id="header">
    <div class="mui-appbar mui--appbar-line-height">
      <div class="mui-container-fluid">
        <a
          class="sidedrawer-toggle mui--visible-xs-inline-block mui--visible-sm-inline-block js-show-sidedrawer">☰</a>
        <a
          class="sidedrawer-toggle mui--hidden-xs mui--hidden-sm js-hide-sidedrawer">☰</a>
        <span class="mui--text-title mui--visible-xs-inline-block">Brand.io</span>
      </div>
    </div>
  </header>
  <div id="content-wrapper">
    <!-- Main content goes here -->
  </div>
  <footer id="footer">
    <div class="mui-container-fluid">
      <br> Source on GitHub
    </div>
  </footer>
</body>
</html>