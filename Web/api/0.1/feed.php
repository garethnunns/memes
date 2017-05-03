<?php
	require_once '../../site/functions.php';

	echo json_encode(memeFeed($_POST['key'],$_POST['page'],$_POST['thumb'],$_POST['full']));
?>