if [[ ! -z echo $PATH | grep '/home/*/bin' ]]; then
    echo << EOF | cat > ~/bin/ipct-server
    echo testing a113
    EOF
fi
