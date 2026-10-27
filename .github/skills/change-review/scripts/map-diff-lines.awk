# Reads a unified diff and prints "file|line|content" for each ADDED line
# (RIGHT side of the PR). The line number is the one in the new file.
#
# Usage: awk -f map-diff-lines.awk pr.diff > map.txt

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
  # @@ -a,b +c,d @@
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
