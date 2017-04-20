<?php
	require_once 'site/web.php';
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo $sitename; ?> · Page not found</title>

		<?php include 'site/head.php'; ?>
	</head>

	<body>
		<?php include 'site/header.php'; ?>

		<div class="wrapper">
<?php
	echo $pageError;
?>
		</div>

		<?php include 'site/footer.php'; ?>
	</body>
</html>