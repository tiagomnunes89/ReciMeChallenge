
/^diff --git / { file = ""; next }
/^\+\+\+ / {
  path = $0
  sub(/^\+\+\+ /, "", path)
  if (path == "/dev/null") { file = ""; next }
  sub(/^b\//, "", path)
  file = path
  next
}
/^@@ / {
  hunk = $0
  sub(/^@@ -[0-9]+(,[0-9]+)? \+/, "", hunk)
  sub(/[ ,].*$/, "", hunk)
  line = hunk + 0
  inhunk = 1
  next
}
inhunk && file != "" {
  c = substr($0, 1, 1)
  if (c == "+") { print file "|" line "|" substr($0, 2); line++ }
  else if (c == "-") { }
  else if (c == "\\") { }
  else { line++ }
}
